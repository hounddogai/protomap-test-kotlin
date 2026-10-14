package com.acme.transport.network.cutoff.cutoffclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalCutoffClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalCutoffClaimStore()
        val item = newSeasonalCutoffClaim("code-917", "name-102")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
