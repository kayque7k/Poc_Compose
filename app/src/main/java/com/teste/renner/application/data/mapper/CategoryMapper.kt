package com.teste.renner.application.data.mapper

import com.teste.renner.application.data.response.CategoryResponse
import com.teste.renner.application.domain.model.Category

object CategoryMapper {
    fun List<CategoryResponse>.toCategoryList() = map {
        it.toCategory()
    }

    fun CategoryResponse.toCategory() = Category(
        id = id,
        image = image,
        category = category
    )
}