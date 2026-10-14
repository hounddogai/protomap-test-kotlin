package com.acme.transport.network.servicearea.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientServiceAreaThresholdTest {
    @Test
    fun `starts in proposed`() {
        val item = newLenientServiceAreaThreshold("code-219", "label-772")
        assertEquals(LenientServiceAreaThresholdStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLenientServiceAreaThreshold("code-219", "label-772")
        try {
            item.moveTo(LenientServiceAreaThresholdStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientServiceAreaThresholdStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLenientServiceAreaThreshold("code-219", "label-772")
        item.status = LenientServiceAreaThresholdStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LenientServiceAreaThresholdStatus.PROPOSED) }
    }
}
