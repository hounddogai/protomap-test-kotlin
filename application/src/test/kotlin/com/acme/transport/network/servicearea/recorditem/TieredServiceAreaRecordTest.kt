package com.acme.transport.network.servicearea.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredServiceAreaRecordTest {
    @Test
    fun `starts in open`() {
        val item = newTieredServiceAreaRecord("code-876", "name-541")
        assertEquals(TieredServiceAreaRecordStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newTieredServiceAreaRecord("code-876", "name-541")
        try {
            item.moveTo(TieredServiceAreaRecordStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredServiceAreaRecordStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTieredServiceAreaRecord("code-876", "name-541")
        item.status = TieredServiceAreaRecordStatus.ARCHIVED
        assertFails("move") { item.moveTo(TieredServiceAreaRecordStatus.OPEN) }
    }
}
