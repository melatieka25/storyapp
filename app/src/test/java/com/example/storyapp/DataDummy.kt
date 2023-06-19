package com.example.storyapp

import com.example.storyapp.response_model.ListStoryItem

object DataDummy {

    fun generateDummyGetStoriesResponse(): List<ListStoryItem> {
        val listStoryItem: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..100) {
            val story = ListStoryItem(
                "photo url $i",
                "created at $i",
                "user $i",
                "description $i",
                i.toString(),
                null,
                null
            )
            listStoryItem.add(story)
        }
        return listStoryItem
    }
}