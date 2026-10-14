package com.acme.transport.crewing.dutyperiod.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodManifestTest {
    @Test
    fun `starts in new`() {
        val item = newDutyPeriodManifest("code-489", "name-818")
        assertEquals(DutyPeriodManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newDutyPeriodManifest("code-489", "name-818")
        try {
            item.moveTo(DutyPeriodManifestStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodManifestStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDutyPeriodManifest("code-489", "name-818")
        item.status = DutyPeriodManifestStatus.FAILED
        assertFails("move") { item.moveTo(DutyPeriodManifestStatus.NEW) }
    }
}
