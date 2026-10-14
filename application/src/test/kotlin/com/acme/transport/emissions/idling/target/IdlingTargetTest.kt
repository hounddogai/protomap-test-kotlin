package com.acme.transport.emissions.idling.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingTargetTest {
    @Test
    fun `starts in created`() {
        val item = newIdlingTarget("code-661", "title-769")
        assertEquals(IdlingTargetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newIdlingTarget("code-661", "title-769")
        try {
            item.moveTo(IdlingTargetStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingTargetStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newIdlingTarget("code-661", "title-769")
        item.status = IdlingTargetStatus.RETIRED
        assertFails("move") { item.moveTo(IdlingTargetStatus.CREATED) }
    }
}
