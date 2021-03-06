package edu.vanderbilt.cs278.grouppic.repository;

import java.util.Collection;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="pictures")
public interface CaptionRepository  extends MongoRepository<Caption, Long> {	
	
	public Collection<Caption> findByCurrentPictureId( long picId);
	
}
