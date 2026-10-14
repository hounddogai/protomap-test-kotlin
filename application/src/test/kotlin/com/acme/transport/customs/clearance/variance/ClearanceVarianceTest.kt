package com.acme.transport.customs.clearance.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceVarianceTest {
    @Test
    fun `starts in proposed`() {
        val item = newClearanceVariance("code-643", "label-257")
        assertEquals(ClearanceVarianceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newClearanceVariance("code-643", "label-257")
        try {
            item.moveTo(ClearanceVarianceStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceVarianceStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newClearanceVariance("code-643", "label-257")
        item.status = ClearanceVarianceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ClearanceVarianceStatus.PROPOSED) }
    }
}
