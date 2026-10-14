package com.acme.transport.crewing.qualification.qualificationoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveQualificationOverrideTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveQualificationOverride("code-230", "title-303")
        assertEquals(AdaptiveQualificationOverrideStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newAdaptiveQualificationOverride("code-230", "title-303")
        try {
            item.moveTo(AdaptiveQualificationOverrideStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveQualificationOverrideStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveQualificationOverride("code-230", "title-303")
        item.status = AdaptiveQualificationOverrideStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveQualificationOverrideStatus.NEW) }
    }
}
