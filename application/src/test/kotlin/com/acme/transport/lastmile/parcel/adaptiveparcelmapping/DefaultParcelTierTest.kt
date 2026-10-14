package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultParcelTierTest {
    @Test
    fun `starts in idle`() {
        val item = newDefaultParcelTier("code-222", "label-668")
        assertEquals(DefaultParcelTierStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDefaultParcelTier("code-222", "label-668")
        try {
            item.moveTo(DefaultParcelTierStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultParcelTierStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDefaultParcelTier("code-222", "label-668")
        item.status = DefaultParcelTierStatus.STOPPED
        assertFails("move") { item.moveTo(DefaultParcelTierStatus.IDLE) }
    }
}
