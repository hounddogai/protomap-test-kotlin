package com.acme.transport.terminal.crossdock.cappeddockestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalCrossDockThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalCrossDockThresholdStore()
        val item = newSeasonalCrossDockThreshold("code-662", "name-409")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
