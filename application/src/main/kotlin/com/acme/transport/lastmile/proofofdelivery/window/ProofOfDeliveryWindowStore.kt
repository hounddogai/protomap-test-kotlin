package com.acme.transport.lastmile.proofofdelivery.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery windows by code. */
interface ProofOfDeliveryWindowStore {
    fun find(code: String): ProofOfDeliveryWindow?
    fun save(item: ProofOfDeliveryWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryWindowStore(
    var items: MutableMap<String, ProofOfDeliveryWindow>,
    var order: MutableList<String>,
) : ProofOfDeliveryWindowStore {
    override fun find(code: String): ProofOfDeliveryWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryWindow) {
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

    override fun list(): MutableList<ProofOfDeliveryWindow> {
        val out = mutableListOf<ProofOfDeliveryWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryWindowStore(): ProofOfDeliveryWindowStore {
    return MemoryProofOfDeliveryWindowStore(
        mutableMapOf<String, ProofOfDeliveryWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryWindowStore(
    var inner: ProofOfDeliveryWindowStore,
    var cache: TtlCache<ProofOfDeliveryWindow>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryWindowStore {
    override fun find(code: String): ProofOfDeliveryWindow? {
        val hit = (cache.get(code) as ProofOfDeliveryWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryWindow> = inner.list()
}

fun newCachedProofOfDeliveryWindowStore(inner: ProofOfDeliveryWindowStore, ttl: Int, clock: Clock): ProofOfDeliveryWindowStore {
    return CachedProofOfDeliveryWindowStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryWindow>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery windows. Empty fields match all. */
data class ProofOfDeliveryWindowQuery(
    var country: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryWindow): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliveryWindow>): MutableList<ProofOfDeliveryWindow> {
        val matched = filter(items) { candidate: ProofOfDeliveryWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryWindows(store: ProofOfDeliveryWindowStore, query: ProofOfDeliveryWindowQuery): MutableList<ProofOfDeliveryWindow> {
    return query.apply(store.list())
}
