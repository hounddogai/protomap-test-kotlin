package com.acme.transport.lastmile.redelivery.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredRedeliveryOverrideTest {
    @Test
    fun `starts in requested`() {
        val item = newTieredRedeliveryOverride("code-969", "name-555")
        assertEquals(TieredRedeliveryOverrideStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTieredRedeliveryOverride("code-969", "name-555")
        try {
            item.moveTo(TieredRedeliveryOverrideStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredRedeliveryOverrideStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTieredRedeliveryOverride("code-969", "name-555")
        item.status = TieredRedeliveryOverrideStatus.EXPIRED
        assertFails("move") { item.moveTo(TieredRedeliveryOverrideStatus.REQUESTED) }
    }
}
