package com.acme.transport.linehaul.tender.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender targets by code. */
interface TenderTargetStore {
    fun find(code: String): TenderTarget?
    fun save(item: TenderTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderTargetStore(
    var items: MutableMap<String, TenderTarget>,
    var order: MutableList<String>,
) : TenderTargetStore {
    override fun find(code: String): TenderTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderTarget) {
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

    override fun list(): MutableList<TenderTarget> {
        val out = mutableListOf<TenderTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderTargetStore(): TenderTargetStore {
    return MemoryTenderTargetStore(mutableMapOf<String, TenderTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderTargetStore(
    var inner: TenderTargetStore,
    var cache: TtlCache<TenderTarget>,
    var hits: Int,
    var misses: Int,
) : TenderTargetStore {
    override fun find(code: String): TenderTarget? {
        val hit = (cache.get(code) as TenderTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderTarget> = inner.list()
}

fun newCachedTenderTargetStore(inner: TenderTargetStore, ttl: Int, clock: Clock): TenderTargetStore {
    return CachedTenderTargetStore(
        inner = inner,
        cache = newTtlCache<TenderTarget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender targets. Empty fields match all. */
data class TenderTargetQuery(
    var country: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderTarget): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<TenderTarget>): MutableList<TenderTarget> {
        val matched = filter(items) { candidate: TenderTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderTargets(store: TenderTargetStore, query: TenderTargetQuery): MutableList<TenderTarget> {
    return query.apply(store.list())
}
