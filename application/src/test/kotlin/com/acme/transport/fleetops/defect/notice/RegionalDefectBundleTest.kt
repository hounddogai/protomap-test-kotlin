package com.acme.transport.fleetops.defect.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDefectBundleTest {
    @Test
    fun `starts in open`() {
        val item = newRegionalDefectBundle("code-246", "label-786")
        assertEquals(RegionalDefectBundleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newRegionalDefectBundle("code-246", "label-786")
        try {
            item.moveTo(RegionalDefectBundleStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDefectBundleStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRegionalDefectBundle("code-246", "label-786")
        item.status = RegionalDefectBundleStatus.ARCHIVED
        assertFails("move") { item.moveTo(RegionalDefectBundleStatus.OPEN) }
    }
}
