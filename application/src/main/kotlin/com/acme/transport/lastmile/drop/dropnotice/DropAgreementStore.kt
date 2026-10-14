package com.acme.transport.lastmile.drop.dropnotice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop agreements by code. */
interface DropAgreementStore {
    fun find(code: String): DropAgreement?
    fun save(item: DropAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropAgreementStore(
    var items: MutableMap<String, DropAgreement>,
    var order: MutableList<String>,
) : DropAgreementStore {
    override fun find(code: String): DropAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropAgreement) {
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

    override fun list(): MutableList<DropAgreement> {
        val out = mutableListOf<DropAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropAgreementStore(): DropAgreementStore {
    return MemoryDropAgreementStore(mutableMapOf<String, DropAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropAgreementStore(
    var inner: DropAgreementStore,
    var cache: TtlCache<DropAgreement>,
    var hits: Int,
    var misses: Int,
) : DropAgreementStore {
    override fun find(code: String): DropAgreement? {
        val hit = (cache.get(code) as DropAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropAgreement> = inner.list()
}

fun newCachedDropAgreementStore(inner: DropAgreementStore, ttl: Int, clock: Clock): DropAgreementStore {
    return CachedDropAgreementStore(
        inner = inner,
        cache = newTtlCache<DropAgreement>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop agreements. Empty fields match all. */
data class DropAgreementQuery(
    var category: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropAgreement): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropAgreement>): MutableList<DropAgreement> {
        val matched = filter(items) { candidate: DropAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropAgreements(store: DropAgreementStore, query: DropAgreementQuery): MutableList<DropAgreement> {
    return query.apply(store.list())
}
