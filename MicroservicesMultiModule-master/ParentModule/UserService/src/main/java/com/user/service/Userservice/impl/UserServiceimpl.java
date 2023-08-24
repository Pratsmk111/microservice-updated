package com.user.service.Userservice.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.user.service.Userservice.Entities.Hotel;
import com.user.service.Userservice.Entities.Rating;
import com.user.service.Userservice.Entities.User;
import com.user.service.Userservice.ExternalServices.Hotelservices;
import com.user.service.Userservice.Repository.UserRepository;
import com.user.service.Userservice.Service.Userservice;

@Service
public class UserServiceimpl implements Userservice {
	
	//This is for resttemplate
	@Autowired
	private RestTemplate resttemplate;

	@Autowired
	private UserRepository repo;
	
	//this is for feign client
	@Autowired
	private Hotelservices hotelservice;
	
	
	private org.slf4j.Logger logger=LoggerFactory.getLogger(UserServiceimpl.class);
	
	@Override
	public User saveUser(User user) {
		//generate random userId
		String randomuserId=UUID.randomUUID().toString();
		user.setUserId(randomuserId);
		return repo.save(user);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> getallUser() {	 
		List<User>users= repo.findAll();
		
		for(User user:users) {
			@SuppressWarnings("rawtypes")
			ArrayList ratingsofuser= resttemplate.getForObject("http://RATINGSERVICE/ratings/get/user/"+user.getUserId(), ArrayList.class);
		    logger.info("{}",ratingsofuser);  
		    user.setRating(ratingsofuser);
		    
		}
		return users;
	}

	@Override
	public User getUser(String userId) {
	    Optional<User> optionalUser =repo.findById(userId);
	    User user = optionalUser.orElse(null);
	   
	    Rating[] ratingsofuser= resttemplate.getForObject("http://RATINGSERVICE/ratings/get/user/"+user.getUserId(), Rating[].class);
	    logger.info("{}",(Object)ratingsofuser);
	    
	    
	    List<Rating>ratings=Arrays.stream(ratingsofuser).toList();
	    
	    List<Rating> ratinglist=ratings.stream().map(rating->{
	    	Hotel hotel=hotelservice.gethotel(rating.getHotelId());
	    	rating.setHotel(hotel);
	    	return rating;
	    	
	    }).collect(Collectors.toList());
	    
	    	user.setRating(ratinglist);
	    	return user; // Return the user or null if not found
	}


	@Override
	public String deleteUser(String userId) {
		repo.deleteById(userId);
		return"User record deleted succesfully";
	}

	@Override
	public User updateUser(User user) {
		return repo.save(user);
	}

}
