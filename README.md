# ProGlove API examples and documentation

This project contains example apps with integration for ProGlove devices on Android using ProGlove SDK and Intent API.

Modules
=======

pgSdkSampleApp
-----------------

This modules contains a sample app for the scanner and display SDK.

pgSdkSampleJavaApp
------------------

This modules contains a sample app for the scanner and display SDK, written n Java.

pgIntentSampleApp
-----------------

This modules contains a sample app for the scanner and display Intent API.

pgSlimIntentSampleApp
-----------------

This modules contains a minimal sample app for the scanner using intent API.

pgSlimIntentSampleJavaApp
-----------------

This modules contains a minimal sample app for the scanner using intent API, written in Java.

How To Run
==========
- clone this repository
- in the root directory create a file named "pg_keystore.properties" with the following content:
```
PROGLOVE_USER={user}
PROGLOVE_PASSWORD={password}
```
- {user} and {password} should be replaced by the credentials that were provided to you for downloading the ProGlove Connect app and the android connect-sdk
- open the project (root directory of this project) in your favourite IDE (we recommend IntelliJ or Android Studio)
- build and run any of the modules
- common problems and solutions
    - for the advanced features like photo-feature and worker-feedback you need an early-access license installed in ProGlove Connect
    - for receiving scan events in the sample apps, you need to set the corresponding integration path in the ProGlove Connect app

Copyright
=========
(c) Workaround GmbH 2019

End User License Agreement
=========
End User License Agreement ProGlove Connect Software

Important: By clicking “Yes. I Agree + Download File”, installing and/or using (“Downloading”) the ProGlove Connect Software accompanying this End User License Agreement (“License Agreement”), you are agreeing to be bound by this License Agreement. Please read this License Agreement before Downloading the ProGlove Connect Software. If you are Downloading the ProGlove Connect Software  on behalf of your employer, you represent that you have the authority to bind your employer to the terms of the License Agreement. If you do not have such authority or if you do not agree with the terms of this License Agreement agreement, do not install or use the ProGlove Connect Software.

1. ProGlove Connect Software
(a) The ProGlove Connect software (including the Mark 2 software installed on your ProGlove devices), documentation and interfaces, as may be updated or replaced by feature enhancements or software updates, accompanying this License Agreement and described as ProGlove Connect Software in the documentation or release note (“ProGlove Connect Software”) are licensed, not sold, to you by Workaround GmbH (“ProGlove”) for use only under the terms of this License Agreement.
(b) ProGlove, at its discretion, may make available future ProGlove Connect Software updates. The ProGlove Connect Software updates, if any, may not necessarily include all existing software features or new features that ProGlove releases. The terms of this License Agreement will govern any ProGlove Connect Software updates provided by ProGlove, unless such ProGlove Connect Software update is accompanied by a separate license agreement, in which case you agree that the terms of that license agreement will govern. ProGlove has no support or maintenance obligations with respect to the ProGlove Connect Software.
(c) The ProGlove Connect Software includes basic features, early access features and premium features (as indicated in the documentation or release note). Until further notice your license to the basic features and early access features is royalty-free but subject to termination with three months prior notice by ProGlove.
(d) A feature is an early access feature, if it is described as Alpha or Beta in the documentation or release note, product documentation or product itself. Your use of early access features is time-based and on your own risk.
(e) Your license to premium features is time-based and subject to you (i) signing a ProGlove order form, (ii) paying the license fees and, if applicable, the maintenance & support fees specified in the ProGlove order form, (iii) complying with the time, volume and device type limitations specified in the ProGlove order form and (iv) not sharing your license key with entities not specified in the ProGlove order form. Please note you are strictly prohibited from sharing your license key with entities not specified in the ProGlove order form.

2. Permitted License Uses and Restrictions
(a) Subject to the terms and conditions of this License Agreement, you are granted a limited non-exclusive license to use the ProGlove Connect Software on your Android mobile smartphone and/or tablet for use with ProGlove devices only.
(b) You are not permitted to:
Edit, alter, modify, adapt, translate or otherwise change the whole or any part of the ProGlove Connect Software, or permit the whole or any part of the ProGlove Connect Software to be combined with or become incorporated in any other software, or decompile, disassemble or reverse engineer the ProGlove Connect Software (or the Mark 2 software on your ProGlove device) or attempt to do any such things (except as and only to the extent any foregoing restriction is prohibited by applicable law or by licensing terms governing the use of open source components that may be included with the ProGlove Connect Software),
Rent, lease, lend, sell, redistribute, or sublicense the ProGlove Connect Software,
Allow any third party to use the ProGlove Connect Software on behalf of or for the benefit of any third party,
Use the ProGlove Connect Software for any purpose that ProGlove considers is a breach of this License Agreement, or
Remove, obscure, or alter any proprietary notices (including trademark and copyright notices) that may be affixed to or contained within the ProGlove Connect Software.
(c) You agree to use the ProGlove Connect Software in compliance with all applicable laws, including local laws of the country or region in which you reside or in which you download or use the ProGlove Connect Software. Download of the ProGlove Connect Software requires a unique user name and password combination. You will use commercially reasonable efforts to prevent unauthorized access to or use of the ProGlove Connect Software, and notify ProGlove promptly of any such unauthorized access or use.

3. Authorized Partners
If you are an authorized ProGlove distribution partner, your customers still have to receive a unique user name and password combination and complete the registration form in order to accept the License Agreement, download and use the ProGlove Connect Software. If you are an authorized ProGlove distribution Partner, you may use the ProGlove Connect Software for showcase purposes (but you may not replaces the ProGlove logo with your logo in the ProGlove Connect Software). You may also not share your license key with your customers.
4. Ownership
ProGlove and its licensors retain ownership of the ProGlove Connect Software and reserve all rights not expressly granted to you. ProGlove reserves the right to grant licences to use the ProGlove Connect Software to third parties.

5. Feedback
ProGlove may use and include any feedback that you provide to improve the ProGlove Connect Software or other ProGlove products or technologies. All feedback becomes the sole property of ProGlove and may be used in any manner ProGlove sees fit. ProGlove has no obligation to respond to feedback or to incorporate feedback into the ProGlove Connect Software. ProGlove may also use the feedback that you provide to provide notices to you which may be of use or interest to you.

6. Termination
This License Agreement is effective until terminated. Your rights under this License Agreement will terminate automatically without notice from ProGlove if you fail to comply with any term(s) of this License Agreement. Upon the termination of this License Agreement, you shall cease all use of the ProGlove Connect Software. Sections 4,5,7,8, and 11 of this License Agreement shall survive any such termination.

7. Disclaimer of Warranties
(a) YOU EXPRESSLY ACKNOWLEDGE AND AGREE THAT, TO THE EXTENT PERMITTED BY APPLICABLE LAW, USE OF THE PROGLOVE CONNECT SOFTWARE IS AT YOUR SOLE RISK AND THAT THE ENTIRE RISK AS TO SATISFACTORY QUALITY, PERFORMANCE, ACCURACY AND EFFORT IS WITH YOU.
(B) TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW, THE PROGLOVE CONNECT SOFTWARE IS PROVIDED “AS IS” AND “AS AVAILABLE” AND WITHOUT WARRANTY OF ANY KIND.
(C) YOU FURTHER ACKNOWLEDGE THAT THE PROGLOVE CONNECT SOFTWARE IS NOT SUITABLE FOR USE IN SITUATIONS OR ENVIRONMENTS WHERE THE FAILURE OR TIME DELAYS OF, OR ERRORS OR INACCURACIES IN, THE CONTENT, DATA OR INFORMATION PROVIDED BY THE PROGLOVE CONNECT SOFTWARE COULD LEAD TO DEATH, PERSONAL INJURY, OR SEVERE PHYSICAL OR ENVIRONMENTAL DAMAGE.

8. Limitation of Liability
ProGlove shall be liable for any of your damages resulting from grossly negligent or intentional behavior of ProGlove, which are due to culpable injury to life, body, and health, or which arise due to the assumption of a guarantee or according to the German Product Liability Act. In all other cases, ProGlove’s liability for damages is limited to the infringement of material obligations of the agreement. Material obligations are only such obligations which fulfillments allow the proper execution of the agreement in the first place and where you may rely on the compliance with these obligations. ProGlove’s liability for the loss of data is limited to the typical expenditures required for the restoration thereof, which are normal and typical if security copies have been made. ProGlove’s liability in case of negligent infringement of material obligations of the agreement by ProGlove shall be limited to foreseeable damages which are typical for this type of contract. A strict liability of ProGlove for defects existing at the time of entering into this License Agreement is hereby expressly excluded. All claims against ProGlove for damages shall be statute barred 6 months after download. This shall not apply to any claims in tort. The foregoing limitations of liability also apply with regard to all ProGlove’s representatives, including but not limited to its directors, legal representatives, employees, and other vicarious agents.

9. Export Control
You may not use or otherwise export or re-export the ProGlove Connect Software except as authorized by German law and the laws of the jurisdiction(s) in which the ProGlove Connect Software was obtained. In particular, but without limitation, the ProGlove Connect Software may not be exported or re-exported (a) into any U.S. embargoed countries or (b) to anyone on the U.S. Treasury Department’s list of Specially Designated Nationals or the U.S. Department of Commerce Denied Person’s List or Entity List or any other restricted party lists.

10. Third Party Software Components
Portions of the ProGlove Connect Software may utilize or include third party software and other copyrighted material. Acknowledgements, licensing terms and disclaimers for such material are contained in the electronic documentation for the ProGlove Connect Software (and for the Mark 2 software in the documentation for your ProGlove devices), and your use of such material is governed by their respective terms. If a software component is described as open source in the documentation, the relevant open source license terms apply.

11. Controlling Law
This License Agreement will be governed by and construed in accordance with the laws of Germany, excluding its conflict of law principles and excluding the United Nations Convention on Contracts for the International Sale of Goods. If for any reason a court of competent jurisdiction finds any provision, or portion thereof, to be unenforceable, the remainder of this License Agreement shall continue in full force and effect.

12. Complete Agreement
This License Agreement constitutes the entire agreement between you and ProGlove relating to the ProGlove Connect Software and supersedes all prior or contemporaneous understandings regarding such subject matter. No amendment to or modification of this License Agreement will be binding unless in writing and signed by ProGlove.
