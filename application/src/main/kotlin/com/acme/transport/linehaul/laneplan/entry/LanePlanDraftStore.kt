package com.acme.transport.linehaul.laneplan.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan drafts by code. */
interface LanePlanDraftStore {
    fun find(code: String): LanePlanDraft?
    fun save(item: LanePlanDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanDraftStore(
    var items: MutableMap<String, LanePlanDraft>,
    var order: MutableList<String>,
) : LanePlanDraftStore {
    override fun find(code: String): LanePlanDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanDraft) {
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

    override fun list(): MutableList<LanePlanDraft> {
        val out = mutableListOf<LanePlanDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanDraftStore(): LanePlanDraftStore {
    return MemoryLanePlanDraftStore(mutableMapOf<String, LanePlanDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanDraftStore(
    var inner: LanePlanDraftStore,
    var cache: TtlCache<LanePlanDraft>,
    var hits: Int,
    var misses: Int,
) : LanePlanDraftStore {
    override fun find(code: String): LanePlanDraft? {
        val hit = (cache.get(code) as LanePlanDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanDraft> = inner.list()
}

fun newCachedLanePlanDraftStore(inner: LanePlanDraftStore, ttl: Int, clock: Clock): LanePlanDraftStore {
    return CachedLanePlanDraftStore(
        inner = inner,
        cache = newTtlCache<LanePlanDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan drafts. Empty fields match all. */
data class LanePlanDraftQuery(
    var unit: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanDraft): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LanePlanDraft>): MutableList<LanePlanDraft> {
        val matched = filter(items) { candidate: LanePlanDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanDrafts(store: LanePlanDraftStore, query: LanePlanDraftQuery): MutableList<LanePlanDraft> {
    return query.apply(store.list())
}
