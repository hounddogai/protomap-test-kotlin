package com.acme.transport.lastmile.proofofdelivery.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default proof of delivery ledgers by code. */
interface DefaultProofOfDeliveryLedgerStore {
    fun find(code: String): DefaultProofOfDeliveryLedger?
    fun save(item: DefaultProofOfDeliveryLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultProofOfDeliveryLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultProofOfDeliveryLedgerStore(
    var items: MutableMap<String, DefaultProofOfDeliveryLedger>,
    var order: MutableList<String>,
) : DefaultProofOfDeliveryLedgerStore {
    override fun find(code: String): DefaultProofOfDeliveryLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultProofOfDeliveryLedger) {
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

    override fun list(): MutableList<DefaultProofOfDeliveryLedger> {
        val out = mutableListOf<DefaultProofOfDeliveryLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultProofOfDeliveryLedgerStore(): DefaultProofOfDeliveryLedgerStore {
    return MemoryDefaultProofOfDeliveryLedgerStore(
        mutableMapOf<String, DefaultProofOfDeliveryLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultProofOfDeliveryLedgerStore(
    var inner: DefaultProofOfDeliveryLedgerStore,
    var cache: TtlCache<DefaultProofOfDeliveryLedger>,
    var hits: Int,
    var misses: Int,
) : DefaultProofOfDeliveryLedgerStore {
    override fun find(code: String): DefaultProofOfDeliveryLedger? {
        val hit = (cache.get(code) as DefaultProofOfDeliveryLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultProofOfDeliveryLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultProofOfDeliveryLedger> = inner.list()
}

fun newCachedDefaultProofOfDeliveryLedgerStore(inner: DefaultProofOfDeliveryLedgerStore, ttl: Int, clock: Clock): DefaultProofOfDeliveryLedgerStore {
    return CachedDefaultProofOfDeliveryLedgerStore(
        inner = inner,
        cache = newTtlCache<DefaultProofOfDeliveryLedger>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default proof of delivery ledgers. Empty fields match all. */
data class DefaultProofOfDeliveryLedgerQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultProofOfDeliveryLedger): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultProofOfDeliveryLedger>): MutableList<DefaultProofOfDeliveryLedger> {
        val matched = filter(items) { candidate: DefaultProofOfDeliveryLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultProofOfDeliveryLedgers(store: DefaultProofOfDeliveryLedgerStore, query: DefaultProofOfDeliveryLedgerQuery): MutableList<DefaultProofOfDeliveryLedger> {
    return query.apply(store.list())
}
