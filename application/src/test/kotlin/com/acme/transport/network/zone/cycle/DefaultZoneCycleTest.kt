package com.acme.transport.network.zone.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultZoneCycleTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefaultZoneCycle("code-802", "title-910")
        assertEquals(DefaultZoneCycleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newDefaultZoneCycle("code-802", "title-910")
        try {
            item.moveTo(DefaultZoneCycleStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultZoneCycleStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefaultZoneCycle("code-802", "title-910")
        item.status = DefaultZoneCycleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefaultZoneCycleStatus.PROPOSED) }
    }
}
