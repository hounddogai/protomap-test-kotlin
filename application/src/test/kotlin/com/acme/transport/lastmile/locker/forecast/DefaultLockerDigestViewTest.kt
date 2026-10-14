package com.acme.transport.lastmile.locker.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultLockerDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultLockerDigest("code-142", "name-750")
        val view = toDefaultLockerDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultLockerDigestViews(mutableListOf<DefaultLockerDigest>(item, item)).size, "views")
    }
}
