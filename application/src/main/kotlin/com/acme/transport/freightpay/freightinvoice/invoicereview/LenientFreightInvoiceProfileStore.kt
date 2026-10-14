package com.acme.transport.freightpay.freightinvoice.invoicereview

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient freight invoice profiles by code. */
interface LenientFreightInvoiceProfileStore {
    fun find(code: String): LenientFreightInvoiceProfile?
    fun save(item: LenientFreightInvoiceProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientFreightInvoiceProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientFreightInvoiceProfileStore(
    var items: MutableMap<String, LenientFreightInvoiceProfile>,
    var order: MutableList<String>,
) : LenientFreightInvoiceProfileStore {
    override fun find(code: String): LenientFreightInvoiceProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientFreightInvoiceProfile) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<LenientFreightInvoiceProfile> {
        val out = mutableListOf<LenientFreightInvoiceProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientFreightInvoiceProfileStore(): LenientFreightInvoiceProfileStore {
    return MemoryLenientFreightInvoiceProfileStore(
        mutableMapOf<String, LenientFreightInvoiceProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientFreightInvoiceProfileStore(
    var inner: LenientFreightInvoiceProfileStore,
    var cache: TtlCache<LenientFreightInvoiceProfile>,
    var hits: Int,
    var misses: Int,
) : LenientFreightInvoiceProfileStore {
    override fun find(code: String): LenientFreightInvoiceProfile? {
        val hit = (cache.get(code) as LenientFreightInvoiceProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientFreightInvoiceProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientFreightInvoiceProfile> = inner.list()
}

fun newCachedLenientFreightInvoiceProfileStore(inner: LenientFreightInvoiceProfileStore, ttl: Int, clock: Clock): LenientFreightInvoiceProfileStore {
    return CachedLenientFreightInvoiceProfileStore(
        inner = inner,
        cache = newTtlCache<LenientFreightInvoiceProfile>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient freight invoice profiles. Empty fields match all. */
data class LenientFreightInvoiceProfileQuery(
    var category: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientFreightInvoiceProfile): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientFreightInvoiceProfile>): MutableList<LenientFreightInvoiceProfile> {
        val matched = filter(items) { candidate: LenientFreightInvoiceProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientFreightInvoiceProfiles(store: LenientFreightInvoiceProfileStore, query: LenientFreightInvoiceProfileQuery): MutableList<LenientFreightInvoiceProfile> {
    return query.apply(store.list())
}
