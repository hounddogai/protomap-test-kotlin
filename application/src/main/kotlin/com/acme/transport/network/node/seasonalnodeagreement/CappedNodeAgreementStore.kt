package com.acme.transport.network.node.seasonalnodeagreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps capped node agreements by code. */
interface CappedNodeAgreementStore {
    fun find(code: String): CappedNodeAgreement?
    fun save(item: CappedNodeAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedNodeAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedNodeAgreementStore(
    var items: MutableMap<String, CappedNodeAgreement>,
    var order: MutableList<String>,
) : CappedNodeAgreementStore {
    override fun find(code: String): CappedNodeAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedNodeAgreement) {
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

    override fun list(): MutableList<CappedNodeAgreement> {
        val out = mutableListOf<CappedNodeAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedNodeAgreementStore(): CappedNodeAgreementStore {
    return MemoryCappedNodeAgreementStore(mutableMapOf<String, CappedNodeAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedNodeAgreementStore(
    var inner: CappedNodeAgreementStore,
    var cache: TtlCache<CappedNodeAgreement>,
    var hits: Int,
    var misses: Int,
) : CappedNodeAgreementStore {
    override fun find(code: String): CappedNodeAgreement? {
        val hit = (cache.get(code) as CappedNodeAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedNodeAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedNodeAgreement> = inner.list()
}

fun newCachedCappedNodeAgreementStore(inner: CappedNodeAgreementStore, ttl: Int, clock: Clock): CappedNodeAgreementStore {
    return CachedCappedNodeAgreementStore(
        inner = inner,
        cache = newTtlCache<CappedNodeAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped node agreements. Empty fields match all. */
data class CappedNodeAgreementQuery(
    var title: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedNodeAgreement): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedNodeAgreement>): MutableList<CappedNodeAgreement> {
        val matched = filter(items) { candidate: CappedNodeAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedNodeAgreements(store: CappedNodeAgreementStore, query: CappedNodeAgreementQuery): MutableList<CappedNodeAgreement> {
    return query.apply(store.list())
}
