package com.acme.transport.customs.broker.brokergroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalBrokerOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalBrokerOverrideStore()
        val item = newSeasonalBrokerOverride("code-789", "name-218")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
