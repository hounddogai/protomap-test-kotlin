package com.acme.transport.emissions.idling.idlingcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultIdlingRollupTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultIdlingRollup("code-450", "title-438")
        assertEquals(DefaultIdlingRollupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newDefaultIdlingRollup("code-450", "title-438")
        try {
            item.moveTo(DefaultIdlingRollupStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultIdlingRollupStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultIdlingRollup("code-450", "title-438")
        item.status = DefaultIdlingRollupStatus.FAILED
        assertFails("move") { item.moveTo(DefaultIdlingRollupStatus.NEW) }
    }
}
