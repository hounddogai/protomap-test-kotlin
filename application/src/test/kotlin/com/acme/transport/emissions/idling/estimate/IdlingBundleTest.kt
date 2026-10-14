package com.acme.transport.emissions.idling.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingBundleTest {
    @Test
    fun `starts in draft`() {
        val item = newIdlingBundle("code-200", "label-375")
        assertEquals(IdlingBundleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newIdlingBundle("code-200", "label-375")
        try {
            item.moveTo(IdlingBundleStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingBundleStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newIdlingBundle("code-200", "label-375")
        item.status = IdlingBundleStatus.CLOSED
        assertFails("move") { item.moveTo(IdlingBundleStatus.DRAFT) }
    }
}
