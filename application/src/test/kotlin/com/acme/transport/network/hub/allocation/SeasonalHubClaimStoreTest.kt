package com.acme.transport.network.hub.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalHubClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalHubClaimStore()
        val item = newSeasonalHubClaim("code-507", "title-385")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
