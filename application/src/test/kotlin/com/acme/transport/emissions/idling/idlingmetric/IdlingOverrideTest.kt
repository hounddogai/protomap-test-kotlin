package com.acme.transport.emissions.idling.idlingmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingOverrideTest {
    @Test
    fun `starts in planned`() {
        val item = newIdlingOverride("code-224", "title-643")
        assertEquals(IdlingOverrideStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newIdlingOverride("code-224", "title-643")
        try {
            item.moveTo(IdlingOverrideStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingOverrideStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newIdlingOverride("code-224", "title-643")
        item.status = IdlingOverrideStatus.CANCELLED
        assertFails("move") { item.moveTo(IdlingOverrideStatus.PLANNED) }
    }
}
