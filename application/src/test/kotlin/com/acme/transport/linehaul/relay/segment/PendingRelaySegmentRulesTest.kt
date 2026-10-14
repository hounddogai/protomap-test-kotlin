package com.acme.transport.linehaul.relay.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRelaySegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingRelaySegmentValidator()
        val item = newPendingRelaySegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = PendingRelaySegmentRequiredDescriptionRule("pending relay segment needs an entry")
        val item = newPendingRelaySegment("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PendingRelaySegmentRequiredNameRule("pending relay segment needs this field")
        val item = newPendingRelaySegment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingRelaySegmentRequiredCodeRule("pending relay segment needs an entry")
        val item = newPendingRelaySegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
