package com.acme.transport.crewing.roster.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterDigest("code-333", "name-649")
        val view = toRosterDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterDigestViews(mutableListOf<RosterDigest>(item, item)).size, "views")
    }
}
