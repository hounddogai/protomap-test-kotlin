package com.acme.transport.customs.hold.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldVarianceTest {
    @Test
    fun `starts in open`() {
        val item = newHoldVariance("code-596", "name-371")
        assertEquals(HoldVarianceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newHoldVariance("code-596", "name-371")
        try {
            item.moveTo(HoldVarianceStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldVarianceStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newHoldVariance("code-596", "name-371")
        item.status = HoldVarianceStatus.ARCHIVED
        assertFails("move") { item.moveTo(HoldVarianceStatus.OPEN) }
    }
}
