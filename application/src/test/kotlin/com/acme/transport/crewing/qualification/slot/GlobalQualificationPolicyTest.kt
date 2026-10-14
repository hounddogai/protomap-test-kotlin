package com.acme.transport.crewing.qualification.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalQualificationPolicyTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalQualificationPolicy("code-770", "title-978")
        assertEquals(GlobalQualificationPolicyStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newGlobalQualificationPolicy("code-770", "title-978")
        try {
            item.moveTo(GlobalQualificationPolicyStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalQualificationPolicyStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalQualificationPolicy("code-770", "title-978")
        item.status = GlobalQualificationPolicyStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalQualificationPolicyStatus.CREATED) }
    }
}
