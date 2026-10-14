package com.acme.transport.emissions.idling.idlingforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingProfileTest {
    @Test
    fun `starts in created`() {
        val item = newIdlingProfile("code-805", "label-368")
        assertEquals(IdlingProfileStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newIdlingProfile("code-805", "label-368")
        try {
            item.moveTo(IdlingProfileStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingProfileStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newIdlingProfile("code-805", "label-368")
        item.status = IdlingProfileStatus.RETIRED
        assertFails("move") { item.moveTo(IdlingProfileStatus.CREATED) }
    }
}
