package edu.vanderbilt.cs278.grouppic;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.vanderbilt.cs278.grouppic.client.PictureSvcApi;
import edu.vanderbilt.cs278.grouppic.repository.Caption;
import edu.vanderbilt.cs278.grouppic.repository.CaptionRepository;
import edu.vanderbilt.cs278.grouppic.repository.Picture;
import edu.vanderbilt.cs278.grouppic.repository.PictureRepository;

@RestController
public class PictureController implements PictureSvcApi {
	
	@Autowired
	private PictureRepository pictureRepo;
	@Autowired
	private CaptionRepository captionRepo;
	
	public PictureController(PictureRepository p, CaptionRepository c) {
		pictureRepo = p;
		captionRepo = c;
	}
	public PictureController() {
		
	}
	
	@Override
	@RequestMapping(value="/picture", method=RequestMethod.GET)
	@ResponseBody
	public Collection<Picture> getPictureList() {
		Collection<Picture> pics = pictureRepo.findAll();
		if (pics != null) {
			Collection<Picture> picsForUser = new ArrayList<Picture>(pics.size());
			String curUser = this.getCurrentUser();
			for (Picture pic: pics) {
				if (userAuthorizedToViewPicture(pic, curUser))
					picsForUser.add(pic);
			}
			return picsForUser;
		}
		else
			return pics;
	}

	@Override
	@RequestMapping(value="/picture", method=RequestMethod.POST)
	@ResponseBody
	public Picture sendPicture(@RequestBody Picture p) {
		System.out.println(p.toString());
		return pictureRepo.save(p);
	}

	@Override
	@RequestMapping(value="/picture/{id}", method=RequestMethod.GET)
	@ResponseBody
	public Picture getPictureWithId(@PathVariable("id") long id) {
		Picture pic =  pictureRepo.findOne(id);		
		if (userAuthorizedToViewPicture(pic, this.getCurrentUser()))
			return pic;
		else
			throw new UserAuthenticationError();
	}

	@Override
	@RequestMapping(value="/picture/{id}/comments", method=RequestMethod.GET)
	@ResponseBody
	public Collection<Caption> getComments(@PathVariable("id") long id) {
		return captionRepo.findByCurrentPictureId(id);
	}
	
	//@Override
	@RequestMapping(value="/test", method=RequestMethod.GET)
	@ResponseBody
	public String getTest() {
		return getCurrentUser();
	}

	@Override
	@RequestMapping(value="/picture/{id}/comments", method=RequestMethod.POST)
	public Caption postCaption(Caption c, @PathVariable ("id") long id) {
		c.setPictureId(id);
		return captionRepo.save(c);
	}


	@Override
	@RequestMapping(value="/picture/{id}/comments"+"/{cd}"+"/like", method = RequestMethod.POST)
	public Void likeCaption(@PathVariable ("cd") long cd) {
		captionRepo.findOne(cd).upvote();
		return null;
	}
	@Override

	@RequestMapping(value="/picture/{id}", method=RequestMethod.DELETE)
	public Void deletePicture(long id) {
		pictureRepo.delete(id);
		return null;
	}
	
	protected String getCurrentUser() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal == null) {
			throw new UserAuthenticationError();
		}
		String username;
		try {
			if (principal instanceof UserDetails) {
			   username = ((UserDetails)principal).getUsername();
			} else {
			   username = principal.toString();
			}	
		}
		catch (Exception e) {
			throw new UserAuthenticationError();
		}
		return username;
	}
	
	protected boolean userAuthorizedToViewPicture(Picture pic, String user) {
		return pic.getSender().equals(user) || pic.getRecipients().contains(user);
	}
}
