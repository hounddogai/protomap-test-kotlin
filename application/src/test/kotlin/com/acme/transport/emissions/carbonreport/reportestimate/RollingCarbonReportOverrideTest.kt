package com.acme.transport.emissions.carbonreport.reportestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingCarbonReportOverrideTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingCarbonReportOverride("code-765", "label-183")
        assertEquals(RollingCarbonReportOverrideStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newRollingCarbonReportOverride("code-765", "label-183")
        try {
            item.moveTo(RollingCarbonReportOverrideStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingCarbonReportOverrideStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingCarbonReportOverride("code-765", "label-183")
        item.status = RollingCarbonReportOverrideStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingCarbonReportOverrideStatus.PROPOSED) }
    }
}
