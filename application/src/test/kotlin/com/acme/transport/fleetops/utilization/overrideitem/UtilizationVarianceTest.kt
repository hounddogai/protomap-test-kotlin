package com.acme.transport.fleetops.utilization.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationVarianceTest {
    @Test
    fun `starts in draft`() {
        val item = newUtilizationVariance("code-776", "name-216")
        assertEquals(UtilizationVarianceStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newUtilizationVariance("code-776", "name-216")
        try {
            item.moveTo(UtilizationVarianceStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationVarianceStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newUtilizationVariance("code-776", "name-216")
        item.status = UtilizationVarianceStatus.CLOSED
        assertFails("move") { item.moveTo(UtilizationVarianceStatus.DRAFT) }
    }
}
