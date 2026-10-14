package com.acme.transport.freightpay.accessorial.scheduledaccessorialmetric

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled accessorial drafts by code. */
interface ScheduledAccessorialDraftStore {
    fun find(code: String): ScheduledAccessorialDraft?
    fun save(item: ScheduledAccessorialDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledAccessorialDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledAccessorialDraftStore(
    var items: MutableMap<String, ScheduledAccessorialDraft>,
    var order: MutableList<String>,
) : ScheduledAccessorialDraftStore {
    override fun find(code: String): ScheduledAccessorialDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledAccessorialDraft) {
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

    override fun list(): MutableList<ScheduledAccessorialDraft> {
        val out = mutableListOf<ScheduledAccessorialDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledAccessorialDraftStore(): ScheduledAccessorialDraftStore {
    return MemoryScheduledAccessorialDraftStore(
        mutableMapOf<String, ScheduledAccessorialDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledAccessorialDraftStore(
    var inner: ScheduledAccessorialDraftStore,
    var cache: TtlCache<ScheduledAccessorialDraft>,
    var hits: Int,
    var misses: Int,
) : ScheduledAccessorialDraftStore {
    override fun find(code: String): ScheduledAccessorialDraft? {
        val hit = (cache.get(code) as ScheduledAccessorialDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledAccessorialDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledAccessorialDraft> = inner.list()
}

fun newCachedScheduledAccessorialDraftStore(inner: ScheduledAccessorialDraftStore, ttl: Int, clock: Clock): ScheduledAccessorialDraftStore {
    return CachedScheduledAccessorialDraftStore(
        inner = inner,
        cache = newTtlCache<ScheduledAccessorialDraft>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled accessorial drafts. Empty fields match all. */
data class ScheduledAccessorialDraftQuery(
    var name: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledAccessorialDraft): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledAccessorialDraft>): MutableList<ScheduledAccessorialDraft> {
        val matched = filter(items) { candidate: ScheduledAccessorialDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledAccessorialDrafts(store: ScheduledAccessorialDraftStore, query: ScheduledAccessorialDraftQuery): MutableList<ScheduledAccessorialDraft> {
    return query.apply(store.list())
}
