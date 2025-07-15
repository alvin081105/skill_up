package com.gbsw.gbsw.service;

import com.gbsw.gbsw.entity.Post;
import com.gbsw.gbsw.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public List<Post> searchPosts(String keyword) {
        return postRepository.searchByKeyword(keyword);
    }
}
