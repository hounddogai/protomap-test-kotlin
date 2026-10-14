package com.acme.transport.lastmile.redelivery.redeliverytemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRedeliveryOverrideTest {
    @Test
    fun `starts in new`() {
        val item = newGlobalRedeliveryOverride("code-55", "label-15")
        assertEquals(GlobalRedeliveryOverrideStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newGlobalRedeliveryOverride("code-55", "label-15")
        try {
            item.moveTo(GlobalRedeliveryOverrideStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRedeliveryOverrideStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newGlobalRedeliveryOverride("code-55", "label-15")
        item.status = GlobalRedeliveryOverrideStatus.FAILED
        assertFails("move") { item.moveTo(GlobalRedeliveryOverrideStatus.NEW) }
    }
}
