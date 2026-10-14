package com.acme.transport.network.servicearea.areatarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientServiceAreaReviewTest {
    @Test
    fun `starts in new`() {
        val item = newLenientServiceAreaReview("code-503", "label-473")
        assertEquals(LenientServiceAreaReviewStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newLenientServiceAreaReview("code-503", "label-473")
        try {
            item.moveTo(LenientServiceAreaReviewStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientServiceAreaReviewStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLenientServiceAreaReview("code-503", "label-473")
        item.status = LenientServiceAreaReviewStatus.FAILED
        assertFails("move") { item.moveTo(LenientServiceAreaReviewStatus.NEW) }
    }
}
