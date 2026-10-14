package com.acme.transport.customs.declaration.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultDeclarationReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultDeclarationReview("code-941", "name-180")
        val view = toDefaultDeclarationReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultDeclarationReviewViews(mutableListOf<DefaultDeclarationReview>(item, item)).size, "views")
    }
}
