package com.acme.transport.customs.hold.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldPreferenceTest {
    @Test
    fun `starts in new`() {
        val item = newHoldPreference("code-786", "name-443")
        assertEquals(HoldPreferenceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newHoldPreference("code-786", "name-443")
        try {
            item.moveTo(HoldPreferenceStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldPreferenceStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newHoldPreference("code-786", "name-443")
        item.status = HoldPreferenceStatus.FAILED
        assertFails("move") { item.moveTo(HoldPreferenceStatus.NEW) }
    }
}
