package com.acme.transport.lastmile.redelivery.redeliveryquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalRedeliveryGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalRedeliveryGroupStore()
        val item = newSeasonalRedeliveryGroup("code-451", "label-605")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
