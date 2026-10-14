package com.acme.transport.terminal.dwell.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDwellExceptionTest {
    @Test
    fun `starts in created`() {
        val item = newFallbackDwellException("code-7", "name-1")
        assertEquals(FallbackDwellExceptionStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newFallbackDwellException("code-7", "name-1")
        try {
            item.moveTo(FallbackDwellExceptionStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDwellExceptionStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFallbackDwellException("code-7", "name-1")
        item.status = FallbackDwellExceptionStatus.RETIRED
        assertFails("move") { item.moveTo(FallbackDwellExceptionStatus.CREATED) }
    }
}
