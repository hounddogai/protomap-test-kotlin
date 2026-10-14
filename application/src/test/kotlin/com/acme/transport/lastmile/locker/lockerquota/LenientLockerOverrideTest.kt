package com.acme.transport.lastmile.locker.lockerquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientLockerOverrideTest {
    @Test
    fun `starts in planned`() {
        val item = newLenientLockerOverride("code-699", "title-629")
        assertEquals(LenientLockerOverrideStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLenientLockerOverride("code-699", "title-629")
        try {
            item.moveTo(LenientLockerOverrideStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientLockerOverrideStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLenientLockerOverride("code-699", "title-629")
        item.status = LenientLockerOverrideStatus.CANCELLED
        assertFails("move") { item.moveTo(LenientLockerOverrideStatus.PLANNED) }
    }
}
