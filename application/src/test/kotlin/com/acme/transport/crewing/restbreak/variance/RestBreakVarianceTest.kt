package com.acme.transport.crewing.restbreak.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newRestBreakVariance("code-761", "name-565")
        assertEquals(RestBreakVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newRestBreakVariance("code-761", "name-565")
        try {
            item.moveTo(RestBreakVarianceStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakVarianceStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRestBreakVariance("code-761", "name-565")
        item.status = RestBreakVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(RestBreakVarianceStatus.IDLE) }
    }
}
