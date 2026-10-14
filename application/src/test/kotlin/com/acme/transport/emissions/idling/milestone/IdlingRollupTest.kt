package com.acme.transport.emissions.idling.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingRollupTest {
    @Test
    fun `starts in new`() {
        val item = newIdlingRollup("code-14", "label-849")
        assertEquals(IdlingRollupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newIdlingRollup("code-14", "label-849")
        try {
            item.moveTo(IdlingRollupStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingRollupStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newIdlingRollup("code-14", "label-849")
        item.status = IdlingRollupStatus.FAILED
        assertFails("move") { item.moveTo(IdlingRollupStatus.NEW) }
    }
}
