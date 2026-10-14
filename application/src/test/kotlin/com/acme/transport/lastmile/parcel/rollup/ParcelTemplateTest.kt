package com.acme.transport.lastmile.parcel.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelTemplateTest {
    @Test
    fun `starts in created`() {
        val item = newParcelTemplate("code-607", "name-158")
        assertEquals(ParcelTemplateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newParcelTemplate("code-607", "name-158")
        try {
            item.moveTo(ParcelTemplateStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelTemplateStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newParcelTemplate("code-607", "name-158")
        item.status = ParcelTemplateStatus.RETIRED
        assertFails("move") { item.moveTo(ParcelTemplateStatus.CREATED) }
    }
}
