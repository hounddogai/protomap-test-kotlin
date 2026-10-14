package com.acme.transport.freightpay.accessorial.accessorialpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingAccessorialPolicyTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingAccessorialPolicy("code-943", "label-235")
        assertEquals(RollingAccessorialPolicyStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRollingAccessorialPolicy("code-943", "label-235")
        try {
            item.moveTo(RollingAccessorialPolicyStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingAccessorialPolicyStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingAccessorialPolicy("code-943", "label-235")
        item.status = RollingAccessorialPolicyStatus.STOPPED
        assertFails("move") { item.moveTo(RollingAccessorialPolicyStatus.IDLE) }
    }
}
