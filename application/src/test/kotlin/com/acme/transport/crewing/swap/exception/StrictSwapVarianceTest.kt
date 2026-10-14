package com.acme.transport.crewing.swap.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictSwapVarianceTest {
    @Test
    fun `starts in new`() {
        val item = newStrictSwapVariance("code-940", "title-519")
        assertEquals(StrictSwapVarianceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newStrictSwapVariance("code-940", "title-519")
        try {
            item.moveTo(StrictSwapVarianceStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictSwapVarianceStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictSwapVariance("code-940", "title-519")
        item.status = StrictSwapVarianceStatus.FAILED
        assertFails("move") { item.moveTo(StrictSwapVarianceStatus.NEW) }
    }
}
