package com.example.travelmantics

import android.os.Parcel
import android.os.Parcelable



data class TravelDeal(
    var id : String = "",
    var imageUrl : String = "",
    var title : String = "",
    var price : String = "",
    var imageName : String = "",
    var description: String = "") : Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(imageUrl)
        parcel.writeString(title)
        parcel.writeString(price)
        parcel.writeString(imageName)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TravelDeal> {
        override fun createFromParcel(parcel: Parcel): TravelDeal {
            return TravelDeal(parcel)
        }

        override fun newArray(size: Int): Array<TravelDeal?> {
            return arrayOfNulls(size)
        }
    }

}