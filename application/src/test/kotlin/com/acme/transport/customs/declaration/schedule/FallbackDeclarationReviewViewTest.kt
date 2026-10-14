package com.acme.transport.customs.declaration.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackDeclarationReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackDeclarationReview("code-37", "name-88")
        val view = toFallbackDeclarationReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackDeclarationReviewViews(mutableListOf<FallbackDeclarationReview>(item, item)).size, "views")
    }
}
