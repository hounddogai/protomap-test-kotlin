package com.acme.transport.linehaul.backhaul.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingBackhaulQuotaTest {
    @Test
    fun `starts in created`() {
        val item = newRollingBackhaulQuota("code-125", "label-305")
        assertEquals(RollingBackhaulQuotaStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRollingBackhaulQuota("code-125", "label-305")
        try {
            item.moveTo(RollingBackhaulQuotaStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingBackhaulQuotaStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingBackhaulQuota("code-125", "label-305")
        item.status = RollingBackhaulQuotaStatus.RETIRED
        assertFails("move") { item.moveTo(RollingBackhaulQuotaStatus.CREATED) }
    }
}
