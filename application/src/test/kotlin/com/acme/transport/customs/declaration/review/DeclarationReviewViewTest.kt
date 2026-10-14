package com.acme.transport.customs.declaration.review

import kotlin.test.Test
import kotlin.test.assertEquals

class DeclarationReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDeclarationReview("code-998", "title-623")
        val view = toDeclarationReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDeclarationReviewViews(mutableListOf<DeclarationReview>(item, item)).size, "views")
    }
}
